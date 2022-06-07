import React, {useState} from 'react';
import {Text, View, StyleSheet, TouchableOpacity} from 'react-native';
import NfcManager from 'react-native-nfc-manager';
import ScanNFC from './components/ScanNFC';

const App = () => {
  const [hasNfc, setHasNfc] = useState<any | null>(null);
  const [enabled, setEnabled] = React.useState<any | null>();

  React.useEffect(() => {
    (async function checkNfc() {
      try {
        const supported = await NfcManager.isSupported();
        setHasNfc(supported);
        if (supported) {
          await NfcManager.start();
          setEnabled(await NfcManager.isEnabled()); //remove for ios
        }
      } catch (e) {
        console.error('Error caught in init: ', e);
      }
    })();
  }, []);

  if (hasNfc == null) {
    return null;
  } else if (!hasNfc) {
    return (
      <View style={styles.txt}>
        <Text>You device doesn't support NFC</Text>
      </View>
    );
  } else if (!enabled) {
    //works for android
    return (
      <View style={styles.txt}>
        <Text>NFC is not enabled</Text>
        <TouchableOpacity
          onPress={() => {
            NfcManager.goToNfcSetting();
          }}>
          <Text>Press here to go to NFC setting</Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={async () => {
            setEnabled(await NfcManager.isEnabled());
          }}>
          <Text>Check again</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return <ScanNFC />;
};

const styles = StyleSheet.create({
  txt: {
    textAlign: 'center',
    paddingTop: 90,
  },
});
export default App;
