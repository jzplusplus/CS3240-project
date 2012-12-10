set -v
#!/bin/bash
cd ../../src
java test/InterpreterLL1Test ../test/test3/script.txt ../test/test3/grammar.txt ../test/test3/
#java test/InterpreterTest ../test/test3/script.txt ../test/test3/