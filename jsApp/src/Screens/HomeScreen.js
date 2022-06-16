import * as React from 'react';
import {
  View,
  Image,
  Platform,
  Dimensions,
  StatusBar,
  NativeModules,
} from 'react-native';
import NfcProxy from '../NfcProxy';
import {Button} from 'react-native-paper';
import WriteButton from '../Components/WriteButton';

class HomeScreen extends React.Component {
  render() {
    let {navigation} = this.props;
    const padding = 40;
    const width = Dimensions.get('window').width - 2 * padding;
    const {NfcReaderModule} = NativeModules;

    return (
      <>
        <StatusBar barStyle="dark-content" />
        <View style={{flex: 1, padding}}>
          <View
            style={{
              flex: 3,
              justifyContent: 'center',
              alignItems: 'center',
            }}>
            <Image
              source={require('../../images/nfc-512.png')}
              style={{width, height: width}}
              resizeMode="contain"
            />
          </View>

          <View
            style={{
              flex: 2,
              alignItems: 'stretch',
              alignSelf: 'center',
              width,
            }}>
            {Platform.OS === 'ios' && (
              <ActionButton
                outlined
                onPress={async () => {
                  const ndefTag = await NfcProxy.readNdefOnce();
                  console.warn('ndefTag', ndefTag);
                  if (ndefTag) {
                    navigation.navigate('TagDetail', {tag: ndefTag});
                  }
                }}>
                READ NDEF
              </ActionButton>
            )}
            <ActionButton
              outlined
              onPress={async () => {
                navigation.navigate('NdefTypeList');
              }}>
              WRITE NDEF
            </ActionButton>
            <ActionButton
              onPress={async () => {
                console.log('[SCAN NFC TAG] starting to read');
                const tag = await NfcProxy.readTag();
                console.log('[SCAN NFC TAG] read data, ', tag);
                if (tag) {
                  navigation.navigate('TagDetail', {tag});
                }
              }}>
              SCAN NFC TAG
            </ActionButton>
            <WriteButton />
            <Button
              onPress={() => {
                NfcReaderModule.TestingNativeModule('test ', 'success');
              }}>
              Android Modules
            </Button>
          </View>
        </View>
      </>
    );
  }
}

function ActionButton(props) {
  const {outlined, ...extraProps} = props;
  return (
    <Button
      mode={outlined ? 'outlined' : 'contained'}
      style={{borderRadius: 8, marginBottom: 10}}
      labelStyle={[{fontSize: 22}, outlined ? {} : {color: 'white'}]}
      {...extraProps}>
      {props.children}
    </Button>
  );
}

export default HomeScreen;
