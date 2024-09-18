

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {

  //Method Channel
  var methodChannel = const MethodChannel("folio_reader_sdk");

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme
            .of(context)
            .colorScheme
            .inversePrimary,
        title: const Text("Folio Reader with Flutter"),
      ),
      body: Center(
        child: TextButton(
          onPressed: _openSdk,
          child: const Text("Open Folio Reader"),
        ),
      ),
    );
  }
  void _openSdk() async {
    try {
      await methodChannel.invokeMethod("openFolioSDK", "");
    } on PlatformException catch (e) {
      debugPrint("Failed to Invoke: '${e.message}'.");
    }
  }
}
