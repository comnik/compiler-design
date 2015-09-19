scan_format:
	.string	"%d"
print_format:
	.string	"%d\n"

.globl	main
main:
    movl    %esp, %ebp

    # scanf("%d", &i)
    subl	$4, %esp        # allocate i on the stack
	pushl	%esp            # &i
	pushl	$scan_format
	call	__isoc99_scanf

    # increment i
    movl    -4(%ebp), %eax
	incl	%eax

    # printf("%d\n", i)
	pushl	%eax            # i
	pushl	$print_format
	call	printf

    movl	%ebp, %esp      # clear stack
