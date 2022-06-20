import React from 'react';
import {NativeModules, Button} from 'react-native';
const {nfcopenreader} = NativeModules;

export const {
  isSensorDetected,
  startReadingFromCgmPatch,
  stopReadingFromCgmPatch,
} = nfcopenreader;

const ModuleButton = () => {
  const func = async () => {
    try {
      console.log('[Module Button]: start reading CGM patch');
      await isSensorDetected();
      console.log('[Module Button]: sensor detected');
      const response = await startReadingFromCgmPatch(0);
      console.log('[Module Button]: response', response);
    } catch (e) {
      console.error('[Module Button]: error', e);
    }
  };

  return (
    <Button title="Module Button" onPress={func}>
      Module Button
    </Button>
  );
};

export default ModuleButton;
