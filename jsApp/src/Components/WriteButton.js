import React from 'react';
import {Button, View, StyleSheet, AsyncStorage} from 'react-native';
import * as RNFS from 'react-native-fs';
import {checkOrGetExternalStorageWritePermissions} from '../Utils/getPerimissions';
import {externalStorageFolderPath} from '../constants';
import NfcProxy from '../NfcProxy';

const handlerWriteTestFile = async () => {
  try {
    console.log('[handlerWriteTestFile] starting to read tag');
    const tag = await NfcProxy.readTag();
    console.log('[handlerWriteTestFile] read data, ', tag);
    if (!tag) {
      return false;
    }

    const permissions = await checkOrGetExternalStorageWritePermissions();
    const d = new Date();

    const year = d.getFullYear();
    const month = d.getMonth().toString().padStart(2, '0');
    const date = d.getDate().toString().padStart(2, '0');
    const hour = d.getHours().toString().padStart(2, '0');
    const minute = d.getMinutes().toString().padStart(2, '0');
    const second = d.getSeconds().toString().padStart(2, '0');
    const currentDateAndTime = year + month + date + hour + minute + second;

    if (permissions) {
      // const path = `${externalStorageFolderPath}/testFile_${Date.now()}.txt`;
      const path = `${externalStorageFolderPath}/testFile_${currentDateAndTime}.txt`;
      console.log(path);
      await RNFS.writeFile(path, tag);
      console.log('test file write success. At: ', path);
      const contents = await RNFS.readFile(path);
      console.log('contents: ', contents);
    }
  } catch (e) {
    console.log('test file write failure.: ', e);
  }
};
const WriteButton = () => {
  return (
    <View>
      <Button title="write test button" onPress={handlerWriteTestFile}>
        Write test data
      </Button>
    </View>
  );
};
export default WriteButton;
