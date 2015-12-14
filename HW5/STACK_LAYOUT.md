# Stack Layout

We followed the usual ia32 ABI, merely adding the
pointer to the recieving instance just before the return
address.

    |                           | higher addresses
    |                           |       Ʌ
    |   arguments in            |       |
    |   reverse declaration     |
    |   order                   |
    |---------------------------|
    |   method receiver         |
    |---------------------------|
    |   return address          |

    |---BEGIN OF CALLEE FRAME---|

    |   caller basepointer      |
    |---------------------------| <- callee basepointer
    |                           |
    |   local variables         |
    |                           |
    |---------------------------|
    |                           |
    |   spilled registers       |       |
    |                           |       V
    |                           | lower addresses, heap
    |                           |

