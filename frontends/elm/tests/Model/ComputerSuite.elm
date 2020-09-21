module Model.ComputerSuite exposing (..)

import Expect
import Fuzz
import Maybe.Extra as MaybeExtra
import Model.Computer as Computer
import Random
import Test exposing (Test, describe, fuzz)


idFromInt : Test
idFromInt =
    describe "idFromInt"
        [ fuzz (Fuzz.intRange 1 Random.maxInt) "accepts strictly positive ints as valid ids" <|
            \id -> Computer.idFromInt id |> MaybeExtra.isJust |> Expect.true "Expected the Id to be a Just(..)"
        , fuzz (Fuzz.intRange Random.minInt 0) "rejects negative ints as invalid ids" <|
            \id -> Computer.idFromInt id |> MaybeExtra.isNothing |> Expect.true "Expected the Id to be a Nothing"
        ]
