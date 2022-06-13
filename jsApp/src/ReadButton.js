import React from 'react';
import {Button, View, StyleSheet} from 'react-native';

const handlerWriteTestFile = () => {
  console.log('test file');

  
};
const ReadButton = () => {
  return (
    <View>
      <Button OnClick={handlerWriteTestFile()}> Write test data</Button>
    </View>
  );
};

const styles = StyleSheet.create({});
