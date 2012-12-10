set -v
#!/bin/bash
cd ../../src
java test/InterpreterLL1Test ../test/test2/script.txt ../test/test2/grammar.txt ../test/test2/
#java test/InterpreterTest ../test/test2/script.txt ../test/test2/