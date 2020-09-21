module Utils.Routing exposing (navigate, navigateWithQueryParams)

import Browser.Navigation as Nav
import Spa.Generated.Route as Route exposing (Route)
import Url.Builder as UrlBuilder exposing (QueryParameter)


navigate : Nav.Key -> Route -> Cmd msg
navigate key route =
    navigateWithQueryParams key route []


navigateWithQueryParams : Nav.Key -> Route -> List QueryParameter -> Cmd msg
navigateWithQueryParams key route queryParams =
    let
        url =
            UrlBuilder.relative [ Route.toString route ] queryParams
    in
    Nav.pushUrl key url
