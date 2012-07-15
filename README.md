# Data Validation Toolkit (DVT)

A clojure data validation toolkit, giving the user a minimal framework to
create validators and use theese to validate collections of data and check the return code of
the seperate validation checks.

## Usage

Provided in this section is a short description of the functions, along with examples of their usage.

### Functions

#### check

Creates a validation function with the functions defined name or a new name as key.

The two forms of this function is:

##### [function]

Returns the given function as a validation check, the given function has to only take one
argument, which are the value/entry to check, and it must return a boolean.

The function has to be defined or an exception will be thrown.

##### [function :as key]

Returns the given function as a validation check associated with the
specified key. The rules for the functions arguments and return type is the same as the above.

This variant of the function also allows anonymeus functions to be defined as validation checks.

#### on-field

Takes one or more validation checks and returns a collection of the given validation checks
associated to validate on the specified field.

#### validator

Takes one or more on-field collections or validation checks and returns a validator function, which
can be used to validate a key value structure such as a map or structmap.

#### checks

Takes a validator or validated data entry and returns a list of the checks associated with
the validator, or called on the entry.

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

    (defn valid-zip
      [zip-code]
      (let [zip (str zip-code)]
      (and (every? #(Character/isDigit %)  zip) (= 4 (count zip)))))

    (def validate-person
      (validator
        (on-field :name
          (check #(= "Test" %) :as :named-test))
        (on-field :age
          (check over-18)
          (check (partial is-age 20) :as :is-age-20))))

     (def validate-address
       (validator
         (on-field :zip-code
           (check valid-zip))))

The validator functions above, will validate the entry given, by calling
the validation checks associated with the validator, on the fields they are associated with in
the validator specification.

    (validate-person {:name "Test" :age 26})

The call above returns the same map as it was given, but the returned map has a new entry in
its metadata containing the validation results.

    {:dvt {:is-age-20 false, :over-18 true, :named-test true}}

If more than one validator is run on a entry, the validation results are merged together.
In the example below, the two validations is called after each other, this doesn't need to be the
case you can make one or more validations on a collection and then validation more later, and
it will still merge the data, as long as no two checks has the same associated key.

    (validate-address (validate-person {:name "Test" :age 26 :zip-code 2760}))

The meta data for the above call is:

    {:dvt {:valid-zip true, :named-test true, :over-18 true, :is-age-20 false}}

If you have a collection of data entries, with existing meta data, this data will be intact after the
validation as long as nothing of it is placed under the key :dvt, which is used in the DVT to isolate
the DVT data.

    (meta (validate-person ^{:test-data 1234} {:name "Test" :age 26}))

The meta data for the above call is:

    {:dvt {:named-test true, :over-18 true, :is-age-20 false}, :test-data 1234}

#### Example of tests on validated entry

Theese examples uses the validated entries from the examples above, the single validated entry
is calles "e1" and the double validated entry is called "e2".

You can use the valid? and invalid? function to check result for specific validation checks.

    (valid? :is-age-20 e1) - will yield false
    
    (invalid? :is-age-20 e1) - will yield true

The two test can be used with the normal clojure core to make more complex test, such as the one below

    (every? true? (map #(valid? % e1) [:is-age-29 :over-18 :named-test])) - will yield false
    
    (some false? (map #(invalid? % e1) [:is-age-29 :over-18 :named-test])) - will yield true

The two examples above can be rewritten by using the checks function instead of defining a vector
with the checks, as shown below.

    (every? true? (map #(valid? % e1) (checks e1))) - will yield false
    
    (some false? (map #(invalid? % e1) (checks e1))) - will yield true

The checks function can also be used to just see which checks have been performed on a entry,
or will be performed by a given validator.

    (checks e2) - will return the list: (:valid-zip :named-test :over-18 :is-age-20)

    (checks validate-person) - will return the list: (:named-test :over-18 :is-age-20)

## License

Copyright (C) 2012 Claus Petersen

Distributed under the Eclipse Public License, the same as Clojure.