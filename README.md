# JavaScript Indexer

Super simple JavaScript parser written in Clojure to create a representation of JavaScript function names to implementations.

## Why?

It was written to support the creation of a Kindle dictionary of function names to function implementations for Fogus's _Functional JavaScript_ book. This dictionary can be found [here](http://jakemccrary.com/blog/2013/07/09/releasing-the-functional-javascript-companion/).

## Usage

Project is designed to be run from the command line. Easiest way to do this is to eimply use `lein run`. It takes any number of directories as arguments. It outputs on STDOUT a data stucture representing the parsed JavaScript.

    lein run DIRECTORY > sample.index

## Details

### Output

Output data structure can be read by Clojure's `read-string`. It is a vector of maps. Each map represents a single function.

The maps have two keys, `:entry` and `:definition`. The value for `:entry` is simply a string which is the function's name. The `:definition` value is a vector of strings. Each entry in this vector represents a line of the function named in `:entry`. Whitespace is preserved in the strings in the `:definition` vector.

### Limitations

This is a really simple parser. It only supports extracting functions look like the following examples.

    function isEven(n) {
      return (n % 2) === 0;
    }

    function isEven(n) { return (n%2) === 0 }


## License

See LICENSE.txt.
