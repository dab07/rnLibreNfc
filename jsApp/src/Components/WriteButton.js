import React from 'react';
import {Button, View, StyleSheet, AsyncStorage} from 'react-native';
import * as RNFS from 'react-native-fs';
import {checkOrGetExternalStorageWritePermissions} from '../Utils/getPerimissions';
import { externalStorageFolderPath } from "../constants";
import NfcProxy from "../NfcProxy";

const handlerWriteTestFile = async () => {
  try {
    console.log('test file');
    const tag = await NfcProxy.readTag();
    console.log(tag);
    if (!tag) return false;

    const permissions = await checkOrGetExternalStorageWritePermissions();
    if (permissions) {
      const path = `${externalStorageFolderPath}/testFile_${Date.now()}.txt`;
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
