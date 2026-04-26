# Prototype, render engine, for ESP32 in Java / Alpha 1.6.5-10040014 / build - 594
<img width="1400" height="1009" alt="Screenshot 2026-04-10 at 21 45 27" src="https://github.com/user-attachments/assets/a6f6c7bf-73c0-44e2-b9e7-2a72d5ce562e" />

# About      
>This is a prototype software rendering and animation engine designed specifically for the ESP32. The main purpose of this engine is to allow users to create their own animations and >behaviors for lighting systems or display screens based on addressable LEDs.

# A brief overview of how the engine works
>The core concept of this engine is to control the rendering process using bytecode that calls all graphics functions and the graphics module via callbacks. Currently, there are only 3 opcodes for triggering the rendering process and 2 opcodes service

| ID_opcode | Name | type | payload size | signature payload |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| 0x00  | ReSizeFBO  | Render  | 4 byte | 2 byte (size x), 2 byte (size y) |
| 0x01  | FillColor | Render  | 4 byte | 4 byte (color RGBA) |
| 0x02  | Clear | Render  | 0 byte | None |
| 0x03  | CrateGradientRGBA | Render  | 18 byte | 4 byte (color 1 RGBA), 4 byte (color 2 RGBA), 2 byte (point x1), 2 byte (point y1), 2 byte (point x2), 2 byte (point y2), 2 byte (angle) |
| 0x04  | DRAW_PIXEL | Render  | 8 byte | 4 byte (color 1 RGBA), 2 byte (point x), 2 byte (point y) |
| 0x05  | SQR_DRW | Render  | 12 byte | 4 byte (color 1 RGBA), 2 byte (point x1), 2 byte (point y1), 2 byte (point x2), 2 byte (point y2) |
| 0xF8  | INC | Service | 5 byte | 1 byte (register id 0x00-0x07), 4 byte (int32 value)|
| 0xF8  | CMP | Service | 5 byte | 1 byte (register id 0x00-0x07), 1 byte (comparison id 0x00-0x05),  1 byte (register id 0x00-0x07), 2 bytes (address jmp if comparison is false)|
| 0xF9  | LOAD | Service | 5 byte | 1 byte (register id 0x00-0x07), 4 byte (int32 value) |
| 0xFB  | JMP | Service  | 2 byte | 2 bytes (Address Jamp) |

# Assemble name commands
| name | ams_name | type | args |  example | 
| ------------- | ------------- | ------------- | ------------- |  ------------- | 
| ReSizeFBO  | RS_FBO | command | width (uint16), height (uint16) | RS_FBO 32 32 | 
| FillColor  | FILL_C | command | color RGBA | FILL_C 102030FF | 
| Clear  | CL | command | none | CL | 
| CrateGradientRGBA  | CGL_RGB | command | RGBA, RGBA, pos-x1, pos-y1, pos-x2, pos-y2, angle | CGL_RGB FF0000FF FF00FFFF 0 0 15 15 45 | 
| DRAW_PIXEL (user input x,y) | DRW_PU | command | RGBA, pos-x1, pos-y1 | DRW_PU FF0000FF 10 10 | 
| DRAW_PIXEL (register input x,y) | DRW_PR | command | RGBA, id-reg-for-x1, id-reg-for-y1| DRW_PR FF0000FF R0 R1 | 
| SQR_DRW  | SQR_DRW | command | RGBA, pos-x1, pos-y1, pos-x2, pos-y2 | SQR_DRW FF0000FF 0 0 10 10 | 
| INC | INC | command | register id (R0-R7), value | INC R0 10 | 
| CMP | CMP | command | register id (R0-R7), register (R0-R7), cmp-id, register (R0-R7), @lable jmp | CMP R0 <= R1 MainLoop | 
| LOAD | LOAD | command | register id (R0-R7), value | LOAD R0 100 | 
| JMP | JMP | command | @lable jmp | JMP Mainloop | 
| @lable | @lable  | syntax suger | name_lable_for_jamp | @lable Mainloop | 

# Comparison table
| ID_CMP | type |
| ------------- | ------------- |
| 0x00  | == |
| 0x01  | < |
| 0x02  | > |
| 0x03  | <= |
| 0x04  | >= |
| 0x05  | != |

# Register asm table
| ID_Reg | name asm |
| ------------- | ------------- |
| 0x00  | R0 |
| 0x01  | R1 |
| 0x02  | R2 |
| 0x03  | R3 |
| 0x04  | R4 |
| 0x05  | R5 |
| 0x06 | R6 |
| 0x07  | R7 |

# The structure of each opcode 
| Opcode | Payload size | Payload |                  
| ------------- | ------------- | ------------- |
| 1 byte | 2 bytes (UInt16) Big-end   | N bytes |
| 0x01 | 0x00 0x04  | 0x00 0xFF 0x00 0xFF |

# Networking

> The same logic applies when working with network packets; they are marked with a special byte so that the JavaScript frontend can properly interpret the received packet.The table below shows the bytes used to interpret the packet on the web debugger side

| ID_pkg | type |
| ------------- | ------------- |
| 0xFE  | Frame Buffer | 
| 0xFF  | Code VM | 

> The input points for interacting with the virtual machine are set up in the same way.

| ID_pkg | type |
| ------------- | ------------- |
| 0x00  | setResolutionFBO | 
| 0x01  | setTickRate | 
| 0x02  | getByteCodeVM | 
| 0x03  | LoadByteCode | 
| 0x04  | resetVM | 

