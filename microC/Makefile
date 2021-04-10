TARGET=microcc

default: $(TARGET).native

$TARGET: default

native: $(TARGET).native

%.native:
	ocamlbuild -use-ocamlfind $@
	mv $@ $*

clean:
	ocamlbuild -clean

test: clean default
	@chmod +x ./test.sh
	./test.sh

.PHONY: clean default test