# Jollybaby 红外遥控

给带红外发射器的 Android 手机用的 Jollybaby 遥控器。点一个键，就发一帧标准 NEC。

- 地址固定为 `0x80`
- 16 个键：`8001`–`800A`、`801B`、`801F`、`800C`–`800F`
- 38 kHz，地址、反码地址、命令、反码命令

没有红外发射器的手机会提示，不能发送。

调试包在 [Actions](https://github.com/liushm/jollybaby-ir/actions) 的 Artifacts 里。
