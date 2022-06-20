import React from 'react';
import {NativeModules, Button} from 'react-native';
const {nfcopenreader} = NativeModules;

export const {
  isSensorDetected,
  startReadingFromCgmPatch,
  stopReadingFromCgmPatch,
} = nfcopenreader;
const ModuleButton = () => {
  // const OnPress = () => {
  //   isSensorDetected();
  //   startReadingFromCgmPatch();
  //   stopReadingFromCgmPatch();
  // };
  return (
    <Button title="Module Button" onPress={startReadingFromCgmPatch}>
      Module Button
    </Button>
  );
};

export default ModuleButton;
