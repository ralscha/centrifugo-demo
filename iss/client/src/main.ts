import satIconUrl from './saticon.gif'
import {Loader} from "@googlemaps/js-api-loader"
import {Centrifuge, TransportEndpoint} from "centrifuge";

let centered = false;

const loader = new Loader({
    apiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY,
    version: "weekly"
});

async function main() {
    await loader.importLibrary("core");
    const {Map} = await loader.importLibrary("maps") as google.maps.MapsLibrary;

    const latlng = new google.maps.LatLng(0, 0);
    const mapOptions = {
        center: latlng,
        zoom: 4,
        mapTypeId: google.maps.MapTypeId.SATELLITE
    };

    const map = new Map(document.getElementById("map") as HTMLElement, mapOptions);

    const marker = new google.maps.Marker({
        map: map,
        title: "ISS",
        icon: satIconUrl,
    });

    const flightPlanCoordinates: google.maps.LatLng[] = [];

    const token = await fetchCentrifugoToken();
    const centrifuge = new Centrifuge(transports(), {token});
    centrifuge.connect();

    const sub = centrifuge.newSubscription("iss");
    sub.on('publication', ctx => updateMarker(map, marker, flightPlanCoordinates, ctx.data));
    sub.subscribe();
}

function transports(): TransportEndpoint[] {
    return [
        {
            transport: 'websocket',
            endpoint: `ws://${import.meta.env.VITE_CENTRIFUGO_BASE_ADDRESS}/connection/websocket`
        },
        {
            transport: 'http_stream',
            endpoint: `http://${import.meta.env.VITE_CENTRIFUGO_BASE_ADDRESS}/connection/http_stream`
        },
        {
            transport: 'sse',
            endpoint: `http://${import.meta.env.VITE_CENTRIFUGO_BASE_ADDRESS}/connection/sse`
        }
    ];
}

async function fetchCentrifugoToken(): Promise<string> {
    const response = await fetch(`${import.meta.env.VITE_SERVER_URL}/centrifugo-token`);
    if (!response.ok) {
        throw new Error(`Failed to fetch centrifugo token: ${response.status} ${response.statusText}`);
    }

    return await response.text();
}

function updateMarker(map: google.maps.Map, marker: google.maps.Marker,
                      flightPlanCoordinates: google.maps.LatLng[],
                      position: { latitude: string, longitude: string }) {

    if (flightPlanCoordinates.length >= 2) {
        flightPlanCoordinates.shift();
    }

    const latlng = new google.maps.LatLng(parseFloat(position.latitude), parseFloat(position.longitude));
    marker.setPosition(latlng);
    if (!centered) {
        centered = true;
        map.panTo(latlng);
    }

    flightPlanCoordinates.push(latlng);
    const flightPath = new google.maps.Polyline({
        path: flightPlanCoordinates,
        geodesic: true,
        strokeColor: '#ffff00',
        strokeOpacity: 1.0,
        strokeWeight: 3
    });
    flightPath.setMap(map);
}

main().catch(console.error)

