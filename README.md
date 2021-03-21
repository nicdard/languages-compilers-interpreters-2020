# languages-compilers-interpreters-2020
Assignements and exercises for the course on languages, compilers and interpreters of the University of Pisa. 
Learn more: https://github.com/lillo/compiler-course-unipi/

## Installation

Install opam 2.*, the OCaml package manager.

This project is built and tested against the version 4.10.1 of the OCaml compiler. To install and use it run the following commands:

```bash
$ opam update
$ opam switch create 4.10.1
$ opam switch 4.10.1
```

You can install also `utop` to locally test the code in a convenient way:

```bash
$ opam install -y utop
```

After all of this is done, you should finish the process by running the following command, which should have no output:

```bash
$ eval $(opam env)
```

**At this point, close anything you are working on and restart your computer.**

## VSCode Users

the installation of the recommended extensions for this workspace is encouraged. You can view them by pressing `CTRL-Shift-P` and typing `Show Recommended Extensions`.

Also, remember to install LSP server for OCaml using:
```bash
$ opam install ocaml-lsp-server
```

