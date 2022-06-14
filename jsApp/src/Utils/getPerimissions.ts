import {PermissionsAndroid} from 'react-native';
import * as RNFS from 'react-native-fs';
import {externalStorageFolderPath} from '../constants';

export const checkOrGetExternalStorageWritePermissions = async () => {
  try {
    const granted = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE,
      {
        buttonNegative: undefined,
        buttonNeutral: undefined,
        buttonPositive: '',
        title: 'Permission',
        message: 'MyApp needs to access storage.',
      },
    );

    if (granted === PermissionsAndroid.RESULTS.GRANTED) {
      return true;
    }
  } catch (e) {
    console.error('Something went wrong while getting permissions.: ', e);
    return Promise.reject(e);
  }
};

export const checkOrCreateExternalStorageFolder = async () => {
  try {
    const permissions = await checkOrGetExternalStorageWritePermissions();
    if (permissions) {
      await RNFS.mkdir(externalStorageFolderPath);
      return true;
    }
  } catch (e) {
    console.error(
      'Something went wrong while creating external storage folder.: ', e);
    return Promise.reject(e);
  }
};
