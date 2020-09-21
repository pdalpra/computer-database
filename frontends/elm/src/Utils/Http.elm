module Utils.Http exposing (httpErrorToString)

import Http


httpErrorToString : Http.Error -> String
httpErrorToString error =
    case error of
        Http.BadUrl url ->
            "Unexpected URL: " ++ url

        Http.Timeout ->
            "Request timeout"

        Http.NetworkError ->
            "Network error"

        Http.BadStatus statusCode ->
            "Unexpected status code: " ++ String.fromInt statusCode

        Http.BadBody errorMessage ->
            "JSON deserialization error: " ++ errorMessage
