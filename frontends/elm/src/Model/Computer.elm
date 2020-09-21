module Model.Computer exposing (Computer, Id, decoder, encodeId, idFromInt, idToInt)

import Json.Decode as Decode exposing (Decoder)
import Json.Decode.Extra as DecodeExtra
import Json.Decode.Pipeline as JP
import Json.Encode as Encode
import Model.Company as Company exposing (Company)
import Time exposing (Posix)


type Id
    = Id Int


idFromInt : Int -> Maybe Id
idFromInt rawId =
    if rawId > 0 then
        Just (Id rawId)

    else
        Nothing


idToInt : Id -> Int
idToInt (Id id) =
    id


type alias Computer =
    { id : Id
    , name : String
    , introduced : Maybe Posix
    , discontinued : Maybe Posix
    , company : Maybe Company
    }


encodeId : Id -> Encode.Value
encodeId (Id underlying) =
    Encode.int underlying


decoder : Decoder Computer
decoder =
    Decode.succeed Computer
        |> JP.required "id" (Decode.int |> Decode.map Id)
        |> JP.required "name" Decode.string
        |> decodeMaybe "introduced" DecodeExtra.datetime
        |> decodeMaybe "discontinued" DecodeExtra.datetime
        |> decodeMaybe "company" Company.decoder


decodeMaybe : String -> Decoder a -> Decoder (Maybe a -> b) -> Decoder b
decodeMaybe field valDecoder partialDecoder =
    JP.optional field (valDecoder |> Decode.map Just) Nothing partialDecoder
