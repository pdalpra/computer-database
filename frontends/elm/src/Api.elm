module Api exposing
    ( Order(..)
    , Page
    , Settings
    , Sort(..)
    , UnsavedComputer
    , createComputer
    , defaultFetchComputersParameters
    , fetchCompanies
    , fetchComputer
    , fetchComputers
    , toUnsavedComputer
    , updateComputer
    )

import Http
import HttpBuilder exposing (RequestBuilder)
import Iso8601
import Json.Decode as Decode exposing (Decoder)
import Json.Decode.Pipeline as JP
import Json.Encode as Encode
import Json.Encode.Extra as EncodeExtra
import Model.Company as Company exposing (Company)
import Model.Computer as Computer exposing (Computer)
import Time exposing (Posix)
import Url.Builder as UrlBuilder exposing (QueryParameter)


type alias Settings =
    { backendUrl : String
    , timeout : Int
    }



-----------
-- TYPES --
-----------


type alias UnsavedComputer =
    { id : Maybe Computer.Id
    , name : String
    , introduced : Maybe Posix
    , discontinued : Maybe Posix
    , company : Maybe Company.Id
    }


toUnsavedComputer : Computer -> UnsavedComputer
toUnsavedComputer computer =
    { id = Just computer.id
    , name = computer.name
    , introduced = computer.introduced
    , discontinued = computer.discontinued
    , company = computer.company |> Maybe.map .id
    }



--------------
-- REST API --
--------------


type Sort
    = NameSort
    | IntroducedSort
    | DiscontinuedSort
    | CompanyNameSort


sortToString : Sort -> String
sortToString sort =
    case sort of
        NameSort ->
            "name"

        IntroducedSort ->
            "introduced"

        DiscontinuedSort ->
            "discontinued"

        CompanyNameSort ->
            "companyName"


type Order
    = Ascending
    | Descending


orderToString : Order -> String
orderToString order =
    case order of
        Ascending ->
            "asc"

        Descending ->
            "desc"


type alias FetchComputersParameters =
    { pageNumber : Int
    , pageSize : Int
    , sort : Sort
    , order : Order
    , searchQuery : String
    }


type alias Page a =
    { items : List a
    , page : Int
    , offset : Int
    , total : Int
    }


defaultFetchComputersParameters : FetchComputersParameters
defaultFetchComputersParameters =
    { pageNumber = 1, pageSize = 20, sort = NameSort, order = Descending, searchQuery = "" }


fetchCompanies : (Result Http.Error (List Company) -> msg) -> Settings -> Cmd msg
fetchCompanies toMsg apiSettings =
    baseRequest HttpBuilder.get [ "companies" ] apiSettings
        |> HttpBuilder.withExpect (Company.decoder |> Decode.list |> Http.expectJson toMsg)
        |> HttpBuilder.request


fetchComputers : FetchComputersParameters -> (Result Http.Error (Page Computer) -> msg) -> Settings -> Cmd msg
fetchComputers parameters toMsg apiSettings =
    let
        queryParams =
            [ parameters.pageNumber |> UrlBuilder.int "p"
            , parameters.pageSize |> UrlBuilder.int "n"
            , parameters.sort |> sortToString |> UrlBuilder.string "s"
            , parameters.order |> orderToString |> UrlBuilder.string "d"
            , parameters.searchQuery |> UrlBuilder.string "f"
            ]
    in
    baseRequestWithQueryParams HttpBuilder.get [ "computers" ] queryParams apiSettings
        |> HttpBuilder.withExpect (Computer.decoder |> pageDecoder |> Http.expectJson toMsg)
        |> HttpBuilder.request


fetchComputer : Computer.Id -> (Result Http.Error Computer -> msg) -> Settings -> Cmd msg
fetchComputer id toMsg apiSettings =
    baseRequest HttpBuilder.get [ "computers", Computer.idToInt id |> String.fromInt ] apiSettings
        |> HttpBuilder.withExpect (Computer.decoder |> Http.expectJson toMsg)
        |> HttpBuilder.request


createComputer : UnsavedComputer -> (Result Http.Error Computer -> msg) -> Settings -> Cmd msg
createComputer unsavedComputer toMsg apiSettings =
    baseRequest HttpBuilder.post [ "computers" ] apiSettings
        |> HttpBuilder.withJsonBody (encodeUnsavedComputer unsavedComputer)
        |> HttpBuilder.withExpect (Computer.decoder |> Http.expectJson toMsg)
        |> HttpBuilder.request


updateComputer : Computer.Id -> UnsavedComputer -> (Result Http.Error Computer -> msg) -> Settings -> Cmd msg
updateComputer id unsavedComputer toMsg apiSettings =
    baseRequest HttpBuilder.post [ "computers", Computer.idToInt id |> String.fromInt ] apiSettings
        |> HttpBuilder.withJsonBody (encodeUnsavedComputer unsavedComputer)
        |> HttpBuilder.withExpect (Computer.decoder |> Http.expectJson toMsg)
        |> HttpBuilder.request


baseRequest : (String -> RequestBuilder msg) -> List String -> Settings -> RequestBuilder msg
baseRequest method path apiSettings =
    baseRequestWithQueryParams method path [] apiSettings


baseRequestWithQueryParams : (String -> RequestBuilder msg) -> List String -> List QueryParameter -> Settings -> RequestBuilder msg
baseRequestWithQueryParams method path queryParameters apiSettings =
    UrlBuilder.crossOrigin apiSettings.backendUrl path queryParameters
        |> method
        |> HttpBuilder.withTimeout (toFloat apiSettings.timeout)



----------------------------
-- JSON ENCODERS/DECODERS --
----------------------------


pageDecoder : Decoder a -> Decoder (Page a)
pageDecoder subDecoder =
    Decode.succeed Page
        |> JP.required "items" (subDecoder |> Decode.list)
        |> JP.required "page" Decode.int
        |> JP.required "offset" Decode.int
        |> JP.required "total" Decode.int


encodeUnsavedComputer : UnsavedComputer -> Encode.Value
encodeUnsavedComputer computer =
    Encode.object
        [ ( "id", EncodeExtra.maybe Computer.encodeId computer.id )
        , ( "name", Encode.string computer.name )
        , ( "introduced", EncodeExtra.maybe Iso8601.encode computer.introduced )
        , ( "discontinued", EncodeExtra.maybe Iso8601.encode computer.discontinued )
        , ( "company", EncodeExtra.maybe Company.encodeId computer.company )
        ]
