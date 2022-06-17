import React from 'react';
import {NativeModules, Button} from 'react-native';
import NfcProxy from '../NfcProxy';
const {AndroidLibreAppCode} = NativeModules;

const ModuleButton = () => {
  return (
    <Button
      title="Module Button"
      onPress={async () => {
        const tag = await NfcProxy.readTag();
        if (tag) {
          AndroidLibreAppCode.ScanLibre(tag);
        }
      }}>
      Module Button
    </Button>
  );
};

export default ModuleButton;
