import React from 'react';
import {NativeModules, Button} from 'react-native';
const {AndroidLibreAppCode} = NativeModules;

const ModuleButton = () => {
  return (
    <Button
      title="Module Button"
      onPress={() => {
        AndroidLibreAppCode.ScanLibre();
      }}>
      Module Button
    </Button>
  );
};

export default ModuleButton;
