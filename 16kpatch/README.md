# 16kpatch 使用说明（仅生产 arm64）

## 适用范围
- 基线项目：默认 `ijkplayer`
- 主机环境：Darwin arm64（Apple Silicon）
- NDK：`22.1.7171670`（r22）
- 目标：仅 `arm64-v8a` 生产

## 补丁文件说明
- `ndk_r22_16k_commit.patch`
  - 已更新为当前 `ijkplayer` 目录下的全量改动快照（含脚本、构建参数、16K 对齐、arm64 生产约束、README 等）。
- `ndk_r22_soundtouch.patch`
  - `ijksoundtouch` 的 16K 链接与 STL 兼容补丁。
- `ndk_r22_ijkyuv.patch`
  - `ijkyuv` 的 16K 链接补丁。

## 默认 ijkplayer 项目如何使用（推荐顺序）
1. 获取默认项目
```bash
git clone https://github.com/Bilibili/ijkplayer.git
cd ijkplayer
```

2. 拷贝本目录下 3 个 patch 到任意本地目录（假设为 `/path/to/16kpatch`）

3. 在 `ijkplayer` 根目录应用主补丁
```bash
git apply --check /path/to/16kpatch/ndk_r22_16k_commit.patch
git apply /path/to/16kpatch/ndk_r22_16k_commit.patch
```

4. 分别对第三方目录应用子补丁
```bash
cd ijkmedia/ijksoundtouch
git apply --check /path/to/16kpatch/ndk_r22_soundtouch.patch
git apply /path/to/16kpatch/ndk_r22_soundtouch.patch

cd ../ijkyuv
git apply --check /path/to/16kpatch/ndk_r22_ijkyuv.patch
git apply /path/to/16kpatch/ndk_r22_ijkyuv.patch
```

5. 回到项目根目录，初始化并编译（仅 arm64）
```bash
cd ../../
./init-android-openssl.sh
./init-android.sh

cd android/contrib
./compile-openssl.sh arm64
./compile-ffmpeg.sh arm64

cd ..
./compile-ijk.sh arm64
```

## 验证建议
- Stack Canary：`libijkffmpeg.so` 包含 `__stack_chk_fail@LIBC`
- 16K Page Size：`arm64-v8a` 下 `libijkffmpeg.so/libijksdl.so/libijkplayer.so` 的 `PT_LOAD Align` 应为 `0x4000`

## 注意事项
- 该补丁集只面向 arm64 生产链路，不保证其他 ABI 行为。
- 若目标仓库已存在同名改动，`git apply --check` 可能失败；请先清理冲突或基于干净分支应用。
