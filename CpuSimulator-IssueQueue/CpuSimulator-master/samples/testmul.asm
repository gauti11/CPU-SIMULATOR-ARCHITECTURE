

    movc r1 0
loop:
    add r1 r1 1
    cmp r2 r1 10
    bra r2 ge quit

    ;nop
    call r31 mysubroutine
    ;nop
    jmp loop



mysubroutine:
    out r1
    jmp r31




quit:
    halt
