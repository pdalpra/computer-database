module Model.Company exposing (Company, Id, decoder, encodeId)

import Json.Decode as Decode exposing (Decoder)
import Json.Decode.Pipeline as JP
import Json.Encode as Encode


type Id
    = Id Int


type alias Company =
    { id : Id
    , name : String
    }


decoder : Decoder Company
decoder =
    Decode.succeed Company
        |> JP.required "id" (Decode.int |> Decode.map Id)
        |> JP.required "name" Decode.string


encodeId : Id -> Encode.Value
encodeId (Id id) =
    Encode.int id
