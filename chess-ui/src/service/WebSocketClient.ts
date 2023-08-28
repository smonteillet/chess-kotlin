import {Client, StompSubscription} from '@stomp/stompjs';
export class WebSocketClient {
    stompClient : Client
    subscription : StompSubscription | undefined
    constructor(
        private brokerURL : string,
        private subscribeDestination : string,
        private publishDestination : string,
        private onReceiveDataCallback : (json : any) => void
    ) {
        this.stompClient = new Client({
            brokerURL: this.brokerURL,
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });
    }

    connect() {
        this.stompClient.onConnect = (frame) => {
            this.subscription = this.stompClient.subscribe(this.subscribeDestination, (messageOutput) => {
                this.onReceiveDataCallback(JSON.parse(messageOutput.body));
            });
        };

        this.stompClient.onStompError = function (frame) {
            console.log('Broker reported error: ' + frame.headers['message']);
            console.log('Additional details: ' + frame.body);
        };
        this.stompClient.activate();
    }

    sendData(data: any) {
        this.stompClient.publish({
            destination: this.publishDestination,
            body: JSON.stringify(data),
            skipContentLengthHeader: true,
        });
    }

    disconnect() {
        this.stompClient.deactivate()
    }
}