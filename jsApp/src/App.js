import * as React from 'react';
import {Platform, UIManager} from 'react-native';
import NfcScanAndroid from './Components/NfcScanAndroid';
import Icon from 'react-native-vector-icons/MaterialIcons';
import {Provider as PaperProvider, DefaultTheme} from 'react-native-paper';
import AppNavigator from './AppNavigator';

const CustomDefaultTheme = {
  ...DefaultTheme,
  colors: {
    ...DefaultTheme.colors,
    primary: '#3985cb',
  },
  fonts: {
    ...DefaultTheme.fonts,
    superLight: {...DefaultTheme.fonts.light},
  },
  userDefinedThemeProperty: '',
  animation: {
    ...DefaultTheme.animation,
    customProperty: 1,
  },
};

Icon.loadFont();

class App extends React.Component {
  constructor(props) {
    super();
    // explicitly create redux store
    // enable LayoutAnimation for Android
    if (
      Platform.OS === 'android' &&
      UIManager.setLayoutAnimationEnabledExperimental
    ) {
      UIManager.setLayoutAnimationEnabledExperimental(true);
    }
  }

  render() {
    return (
      <PaperProvider theme={CustomDefaultTheme}>
        {Platform.OS === 'android' && <NfcScanAndroid />}
        <AppNavigator />
      </PaperProvider>
    );
  }
}

export default App;
