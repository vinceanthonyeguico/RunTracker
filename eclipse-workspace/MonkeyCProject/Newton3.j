.class public Newton3
.super java/lang/Object

.field private static _sysin Ljava/util/Scanner;
.field private static number I

;
; Runtime input scanner
;
.method static <clinit>()V

	new	java/util/Scanner
	dup
	getstatic	java/lang/System/in Ljava/io/InputStream;
	invokespecial	java/util/Scanner/<init>(Ljava/io/InputStream;)V
	putstatic	Newton3/_sysin Ljava/util/Scanner;
	return

.limit locals 0
.limit stack 3
.end method

;
; Main class constructor
;
.method public <init>()V
.var 0 is this LNewton3;

	aload_0
	invokespecial	java/lang/Object/<init>()V
	return

.limit locals 1
.limit stack 1
.end method

;
; FUNCTION root
;
.method private static root(D)D

.var 6 is diff D
.var 4 is prev D
.var 2 is r D
.var 8 is root D
.var 0 is x D
;
; 011 r:=1
;
	iconst_1
	i2d
	dstore_2
;
; 012 prev:=0
;
	iconst_0
	i2d
	dstore	4
;
; 014 REPEATr:=(x/r+r)/2;diff:=r-prev;IFdiff<0THENdiff:=-diff;prev:=rUNTIL ...
;
L001:
;
; 015 r:=(x/r+r)/2
;
	dload_0
	dload_2
	ddiv
	dload_2
	dadd
	iconst_2
	i2d
	ddiv
	dstore_2
;
; 016 diff:=r-prev
;
	dload_2
	dload	4
	dsub
	dstore	6
;
; 017 IFdiff<0THENdiff:=-diff
;
	dload	6
	iconst_0
	i2d
	dcmpg
	iflt	L005
	iconst_0
	goto	L006
L005:
	iconst_1
L006:
	ifeq	L004
;
; 017 diff:=-diff
;
	dload	6
	dneg
	dstore	6
	goto	L004
L004:
;
; 018 prev:=r
;
	dload_2
	dstore	4
	dload	6
	ldc2_w	1.000000013351432E-10
	dcmpg
	iflt	L007
	iconst_0
	goto	L008
L007:
	iconst_1
L008:
	ifne	L002
	goto	L001
L002:
;
; 021 root:=r
;
	dload_2
	dstore	8

	dload	8
	dreturn

.limit locals 10
.limit stack 4
.end method

;
; PROCEDURE print
;
.method private static print(ID)V

.var 0 is n I
.var 1 is root D
;
; 026 writeln('The square root of ',n:4,' is ',root:8:4)
;
	getstatic	java/lang/System/out Ljava/io/PrintStream;
	ldc	"The square root of %4d is %8.4f\n"
	iconst_2
	anewarray	java/lang/Object
	dup
	iconst_0
	iload_0
	invokestatic	java/lang/Integer/valueOf(I)Ljava/lang/Integer;
	aastore
	dup
	iconst_1
	dload_1
	invokestatic	java/lang/Double/valueOf(D)Ljava/lang/Double;
	aastore
	invokevirtual	java/io/PrintStream/printf(Ljava/lang/String;[Ljava/lang/Object;)Ljava/io/PrintStream;
	pop

	return

.limit locals 3
.limit stack 7
.end method

;
; MAIN
;
.method public static main([Ljava/lang/String;)V
.var 0 is args [Ljava/lang/String;
.var 1 is _start Ljava/time/Instant;
.var 2 is _end Ljava/time/Instant;
.var 3 is _elapsed J

	invokestatic	java/time/Instant/now()Ljava/time/Instant;
	astore_1

;
; 030 FORnumber:=1TO25DOBEGINprint(number,root(number))END
;
	iconst_1
	putstatic	Newton3/number I
L009:
	getstatic	Newton3/number I
	bipush	25
	if_icmpgt	L010
;
; 031 print(number,root(number))
;
	getstatic	Newton3/number I
	getstatic	Newton3/number I
	i2d
	invokestatic	Newton3/root(D)D
	invokestatic	Newton3/print(ID)V
	getstatic	Newton3/number I
	iconst_1
	iadd
	putstatic	Newton3/number I
	goto	L009
L010:

	invokestatic	java/time/Instant/now()Ljava/time/Instant;
	astore_2
	aload_1
	aload_2
	invokestatic	java/time/Duration/between(Ljava/time/temporal/Temporal;Ljava/time/temporal/Temporal;)Ljava/time/Duration;
	invokevirtual	java/time/Duration/toMillis()J
	lstore_3
	getstatic	java/lang/System/out Ljava/io/PrintStream;
	ldc	"\n[%,d milliseconds execution time.]\n"
	iconst_1
	anewarray	java/lang/Object
	dup
	iconst_0
	lload_3
	invokestatic	java/lang/Long/valueOf(J)Ljava/lang/Long;
	aastore
	invokevirtual	java/io/PrintStream/printf(Ljava/lang/String;[Ljava/lang/Object;)Ljava/io/PrintStream;
	pop

	return

.limit locals 5
.limit stack 10
.end method
