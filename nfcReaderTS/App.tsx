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
const { isSensorDetected, startReadingFromLibre } = nfcReaderTS;

const App = () => {
  const isDarkMode = useColorScheme() === "dark";

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  const handleScanStart = async () => {
    try {
      console.log("[nfcR handleScanStart] Started\n");

      const sensorDetect = await isSensorDetected();
      console.log("[nfcR handleScanStart]: sensor detected", sensorDetect);

      const reading = await startReadingFromLibre(0);
      console.log("[nfcR handleScanStart]: response", reading);
    } catch (e) {
      console.error("[nfcR handleScanStart]: error", e);
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
  scanReading: {

  }
});

export default App;
