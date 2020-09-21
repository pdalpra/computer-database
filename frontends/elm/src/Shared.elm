module Shared exposing
    ( Flags
    , Model
    , Msg
    , init
    , subscriptions
    , update
    , view
    )

import Api exposing (Page)
import Bootstrap.Button as Button
import Bootstrap.Grid as Grid
import Bootstrap.Navbar as Navbar
import Bootstrap.Utilities.Spacing as Spacing
import Browser.Navigation as Nav
import Debouncer.Messages as Debouncer exposing (Debouncer)
import Html exposing (Html, option, select, text)
import Html.Attributes exposing (attribute, class, href, style, title)
import Html.Events exposing (onInput)
import Model.Company exposing (Company)
import Model.Computer exposing (Computer)
import RemoteData exposing (WebData)
import Spa.Document exposing (Document)
import Spa.Generated.Route as Route
import Toasty
import Toasty.Defaults as Toast exposing (Toast)
import Url exposing (Url)
import Url.Builder as UrlBuilder
import Utils.Http as HttpUtils
import Utils.Routing as Routing



-- INIT


type alias Flags =
    ( String, Int )


type alias Model =
    { url : Url
    , key : Nav.Key
    , search : String
    , quickSearchComputers : WebData (List Computer)
    , apiSettings : Api.Settings
    , debouncer : Debouncer Msg
    , toasties : Toasty.Stack Toast
    , navbarState : Navbar.State
    , companies : WebData (List Company)
    }


init : Flags -> Url -> Nav.Key -> ( Model, Cmd Msg )
init ( backendUrl, timeout ) url key =
    let
        ( navbarState, navbarCmd ) =
            Navbar.initialState NavbarChanged

        model =
            { url = url
            , key = key
            , search = ""
            , quickSearchComputers = RemoteData.NotAsked
            , apiSettings = { backendUrl = backendUrl, timeout = timeout }
            , companies = RemoteData.Loading
            , debouncer = Debouncer.debounce 500 |> Debouncer.toDebouncer
            , toasties = Toasty.initialState
            , navbarState = navbarState
            }
    in
    ( model
    , Cmd.batch
        [ navbarCmd
        , Api.fetchCompanies (RemoteData.fromResult >> GotCompaniesList) model.apiSettings
        ]
    )



-- UPDATE


type Msg
    = GotCompaniesList (WebData (List Company))
    | GotQuickSearchComputers (WebData (Page Computer))
    | ComputerFullSearch
    | ComputerQuickSearch String
    | NavbarChanged Navbar.State
    | DebouncerSettled (Debouncer.Msg Msg)
    | Toast (Toasty.Msg Toast)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        GotCompaniesList ((RemoteData.Failure e) as result) ->
            let
                toast =
                    Toast.Error (Just "Could not fetch companies") (HttpUtils.httpErrorToString e)
            in
            ( { model | companies = result }, Cmd.none )
                |> Toasty.addToast toastyConfig Toast toast

        GotCompaniesList (_ as result) ->
            ( { model | companies = result }, Cmd.none )

        GotQuickSearchComputers ((RemoteData.Failure e) as result) ->
            let
                toast =
                    Toast.Error (Just "Could not fetch computers") (HttpUtils.httpErrorToString e)
            in
            ( { model | quickSearchComputers = result |> RemoteData.map .items }, Cmd.none )
                |> Toasty.addToast toastyConfig Toast toast

        GotQuickSearchComputers (_ as result) ->
            ( { model | quickSearchComputers = result |> RemoteData.map .items }, Cmd.none )

        ComputerFullSearch ->
            let
                queryParams =
                    [ UrlBuilder.string "f" model.search ]
            in
            ( model, Routing.navigateWithQueryParams model.key Route.Computers__Top queryParams )

        ComputerQuickSearch search ->
            let
                defaults =
                    Api.defaultFetchComputersParameters

                parameters =
                    { defaults | pageSize = 10, searchQuery = search }
            in
            ( { model | search = search }, Api.fetchComputers parameters (RemoteData.fromResult >> GotQuickSearchComputers) model.apiSettings )

        NavbarChanged state ->
            ( { model | navbarState = state }, Cmd.none )

        DebouncerSettled subMsg ->
            Debouncer.update update updateDebouncer subMsg model

        Toast subMsg ->
            Toasty.update toastyConfig Toast subMsg model


updateDebouncer : Debouncer.UpdateConfig Msg Model
updateDebouncer =
    { mapMsg = DebouncerSettled
    , getDebouncer = .debouncer
    , setDebouncer = \debouncer model -> { model | debouncer = debouncer }
    }


subscriptions : Model -> Sub Msg
subscriptions model =
    Navbar.subscriptions model.navbarState NavbarChanged



-- VIEW


view :
    { page : Document msg, toMsg : Msg -> msg }
    -> Model
    -> Document msg
view { page, toMsg } model =
    { title = page.title
    , body =
        [ navbarView model |> Html.map toMsg
        , toastsView model.toasties |> Html.map toMsg
        , Grid.containerFluid [] page.body
        ]
    }


navbarView : Model -> Html Msg
navbarView model =
    let
        computers =
            model.quickSearchComputers |> RemoteData.withDefault []

        computerSummaryView computer =
            [ text computer.name ]

        searchForm =
            [ dynamicSelect "Search computers..." computers computerSummaryView ComputerQuickSearch
            , Button.button
                [ Button.onClick (ComputerFullSearch |> Debouncer.provideInput |> DebouncerSettled)
                , Button.outlineSuccess
                , Button.attrs [ Spacing.my2, Spacing.my2Sm ]
                ]
                [ text "Search" ]
            ]
    in
    Navbar.config NavbarChanged
        |> Navbar.brand [ href "/computers" ] [ text "Computer database" ]
        |> Navbar.customItems [ Navbar.formItem [ Spacing.my2, Spacing.my0Lg ] searchForm ]
        |> Navbar.view model.navbarState


toastsView : Toasty.Stack Toast -> Html Msg
toastsView toasts =
    Toasty.view toastyConfig Toast.view Toast toasts


toastyConfig : Toasty.Config msg
toastyConfig =
    let
        -- Mostly the defaults but 'top' has been removed, so that it fits in the grid's container.
        containerAttrs =
            [ style "position" "fixed"
            , style "right" "0"
            , style "width" "100%"
            , style "max-width" "400px"
            , style "list-style-type" "none"
            , style "padding" "0"
            , style "margin" "0"
            ]
    in
    Toast.config |> Toasty.containerAttrs containerAttrs |> Toasty.delay 5000


dynamicSelect : String -> List a -> (a -> List (Html Msg)) -> (String -> Msg) -> Html Msg
dynamicSelect placeholder items itemToOption msg =
    select
        [ class "selectpicker"
        , attribute "data-live-search" "true"
        , attribute "data-style" "btn-outline-secondary"
        , title placeholder
        , Spacing.mr2Sm
        , onInput (msg >> Debouncer.provideInput >> DebouncerSettled)
        ]
        (items |> List.map itemToOption |> List.map (option []))
