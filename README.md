# Data Validation Toolkit (DVT)

A clojure data validation toolkit, giving the user a minimal framework to
create validators and use theese to validate collections of data and check the return code of
the seperate validation checks.

## Usage

Provided in this section is a short description of the functions, along with examples of their usage.

### Functions

#### check

Creates a validation function with the functions defined name or a new name.

The two forms of this function is:

##### [function]

Returns the given function as a validation check, the given function has to only take one
argument, which are the value/entry to check, and it must return a boolean.

The function has to be defined or an exception will be thrown.

##### [function :as name]

Returns the given function as a validation check associated with the
specified name. The rules for the functions arguments and return type is the same as the above.

This variant of the function allows anonymeus functions and functions to be defined as
validation checks.

#### on-field

Takes one or more validation checks and returns a collection of the given validation checks
associated to validate on a the given field.

#### validator

Takes one or more on-filed collections or validation checks and returns a validator function, which
can be used to validate a key value structure such as a map or structmap.

#### valid?

A test used to check if a given validation check succeeded, it takes the key for
the given check and a entry to check.

#### invalid?

A test used to check if a given validation check failed, it takes the key for
the given check and a entry to check.

### Examples

#### Example of validator and validation

(defn over-18
  [age]
  (>= age 18))

(defn is-age
  [valid-age age-to-check]
  (= valid-age age-to-check))

(def validate-person
  (validator
    (on-field :name
      (check #(= "Test" %) :as :named-test))
    (on-field :age
      (check over-18)
      (check (partial is-age 20) :as :is-age-20))))

The validator function above will call the validation function, associated with
the key :named-test on the field associated with the key :name in the entries
from the collection validated.

(validate-person {:name "Test" :age 26})

The call above returns the same map as it was given, but the returned map has a new entry in
its metadata containing the validation results.

{:dvt {:is-age-20 false, :over-18 true, :named-test true}}

#### Example of tests on validated entry

This example uses the validate entry from the example above, in this example defined as "e1"

(valid? :is-age-20 e1) - will yield false

(invalid? :is-age-20 e1) - will yield true

The two test can be used with the normal clojure core to make more complex test, such as the one below

(every? true? (map #(valid? % e1) [:is-age-29 :over-18 :named-test])) - will yield false

(some false? (map #(invalid? % e1) [:is-age-29 :over-18 :named-test])) - will yield true

## License

Copyright (C) 2012 Claus Petersen

Distributed under the Eclipse Public License, the same as Clojure.