/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.quantasy.iot.gateway.service.dice.simple;

import ch.quantasy.iot.dice.simple.SimpleDice;
import ch.quantasy.mqtt.gateway.client.GatewayClient;
import java.net.URI;
import java.util.Set;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 *
 * @author reto
 */
public class SimpleDiceService {

    private final SimpleDice dice;
    private final GatewayClient<SimpleDiceServiceContract> gatewayClient;

    public SimpleDiceService(URI mqttURI, String mqttClientName, String instanceName) throws MqttException {
        gatewayClient=new GatewayClient<>(mqttURI, mqttClientName, new SimpleDiceServiceContract(instanceName));
        dice = new SimpleDice();
        gatewayClient.connect();
        gatewayClient.subscribe(gatewayClient.getContract().INTENT + "/#", (topic, payload) -> {
            Set<DiceIntent> intents = gatewayClient.toMessageSet(payload, DiceIntent.class);
            intents.stream().filter((intent) -> (intent.play == true)).map((_item) -> {
                dice.play();
                return _item;
            }).forEachOrdered((_item) -> {
                gatewayClient.readyToPublish(gatewayClient.getContract().EVENT_PLAY, new PlayEvent(dice.getChosenSide()));
            });
        });
        gatewayClient.readyToPublish(gatewayClient.getContract().STATUS_SIDES, new DiceStatus(dice.getAmountOfSides()));

    }

}
