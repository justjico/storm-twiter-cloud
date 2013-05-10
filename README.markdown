#  Storm twitter cloud 

## Running with Leiningen

Install Leiningen by following the installation instructions [here](https://github.com/technomancy/leiningen). The storm-starter build uses Leiningen 1.7.1.

### To run 

```
lein deps
lein compile
java -cp $(lein classpath) storm.starter.TwitterWordCloud username password
