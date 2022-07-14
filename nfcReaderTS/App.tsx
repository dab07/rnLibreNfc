/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * Generated with the TypeScript template
 * https://github.com/react-native-community/react-native-template-typescript
 *
 * @format
 */

import React from "react";
import {
  Button,
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
  NativeModules,
} from "react-native";
import { Colors } from "react-native/Libraries/NewAppScreen";
import Section from "./components/Section";

const { nfcReaderTS } = NativeModules;
const { checkForNfcOnDevice, isSensorDetected, startReadingFromLibre, stopReadingFromLibre, activateCGM } = nfcReaderTS;

const App = () => {
  const isDarkMode = useColorScheme() === "dark";

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  const handleScanStart = async () => {
    try {
      console.log("[nfcR handleScanStart] Started\n");

      const isNfcOnDevice = await checkForNfcOnDevice();
      console.log("[nfcR handleScanStart]: ", isNfcOnDevice);
      const sensorDetect = await isSensorDetected();
      console.log("[nfcR handleScanStart]: sensor detected", sensorDetect);

      const reading = await startReadingFromLibre(0);
      console.log("[nfcR handleScanStart]: response", reading);

      const stopreading = await stopReadingFromLibre();
      console.log("[nfcR handleScanStart] Stopped", stopreading);
    } catch (e) {
      console.error("[nfcR handleScanStart]: error", e);
    }
  };

  const activation = async () => {
    try {
      const activateSensor = await activateCGM();
      console.log("[activation]", activateSensor);
    } catch (e) {
      console.log("[nfcR handleScanStart]: error", e);
    }
  };

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar barStyle={isDarkMode ? "light-content" : "dark-content"} />
      <ScrollView contentInsetAdjustmentBehavior="automatic" style={backgroundStyle}>
        <Text style={styles.title}>Nfc Reader</Text>
        <View
          style={{
            backgroundColor: isDarkMode ? Colors.black : Colors.white,
          }}
        >
          <Section title="Actions">
            <Button title="Scan" onPress={handleScanStart} />
            <Button title={"Tap to activate sensor"} onPress={activation} />
          </Section>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  title: {
    fontSize: 50,
    marginHorizontal: 20,
    marginVertical: 10,
    fontWeight: "200",
  },
});

export default App;
