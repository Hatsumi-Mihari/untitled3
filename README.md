# Java prototype, render engine, for ESP32 / Ver: 1.3.6 Alpha (2403.0131)
<img width="650" height="575" alt="Screenshot 2026-03-24 at 1 55 16 AM" src="https://github.com/user-attachments/assets/d52618d8-4bb4-4497-8b90-eca74890d4e1" />


# About      
>This is a prototype software rendering and animation engine designed specifically for the ESP32. The main purpose of this engine is to allow users to create their own animations and >behaviors for lighting systems or display screens based on addressable LEDs.

# A brief overview of how the engine works
>The core concept of this engine is to control the rendering process using bytecode that calls all graphics functions and the graphics module via callbacks. Currently, there are only 3 opcodes for triggering the rendering process and 2 opcodes service

| ID_opcode | Name | type |
| ------------- | ------------- | ------------- |
| 0x00  | ReSizeFBO  | Render  |
| 0x01  | FillColor | Render  |
| 0x02  | Clear | Render  |
| 0xFA  | JUMP_TO_SET_START | Service  |
| 0xFB  | JMP | Service  |

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

