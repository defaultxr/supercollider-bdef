# Bdef
SuperCollider class to create Buffer definitions, a la Ndef, Pdef, etc.

## Overview

Bdef is a class used to create "Buffer definitions" which are similar in concept to the Node and Pattern definitions that can be created with Ndef and Pdef.

### Advantages

- No duplicate Buffers: if you attempt to use Bdef to load a file that's already loaded, Bdef will simply point you to the Buffer that already contains the file.
- Easy creation of wavetables: similar to how you can load a file, you can supply an envelope instead and it's automatically created as a wavetable for you.
- No need to define a variable to hold the buffer: Bdef has its own internal namespace Dictionary that you reference with the key you gave to the Bdef when creating it. Additionally, you can use a file's name as its own key.
- If you have ffmpeg installed, Bdef will automatically attempt to convert non-wav/aiff files to wav for you, storing the resulting file in a temporary directory.

### Disadvantages

- A Bdef can't be provided directly as a parameter to PlayBuf, etc. You have to call .buffer or .bufnum on the Bdef to get its Buffer object, which can then be used by PlayBuf, etc.
- If you load a Buffer using Bdef, then you will have to free it via Bdef as well, otherwise Bdef will not know it has been freed.
- This quark is still under development and probably has some bugs, in addition to the missing features listed under "Current Issues" below.

## Examples

- Create a Bdef, `Bdef(\foo, 44100);` will create a cleared Buffer with 2 channels and 44100 frames each.
- Get the same Bdef without redefining it: `Bdef(\foo);`
- Get the Bdef's Buffer: `Bdef(\foo).buffer;`
- Load a file: `Bdef(\foo, "~/path/to/file.wav");`
- Load an mp3: `Bdef(\foo, "~/path/to/file.mp3");`
- Load a file without giving it a new name: `Bdef("~/path/to/file.wav");`
- Load a file as a wavetable: `Bdef(\foo, "~/path/to/file.wav", wavetable:true);` - note that this will resize the file to the nearest power of 2 (up to and including 32768).
- Load a file with one channel instead of the default 2: `Bdef(\foo, "~/path/to/file.wav", numChannels:1);`
- Load a file as a wavetable with a specific length: `Bdef(\foo, "~/path/to/file.wav", wavetable:1024);`
- Create a wavetable buffer from an Env: `Bdef(\foo, Env([0.5, 1, 0.5, 0, 0.5], 0.25!4, 0), wavetable:1024);`
- Use the syntax shortcut to load an mp3 into a Buffer without giving it a name: `"~/path/to/file.mp3".b;`

So for example, if you're creating a SynthDef that uses a specific sound and you don't want to have to create a new variable just to store the sound and don't want to have to worry about accidentally loading the sound more than once, try this:

```supercollider
SynthDef(\hihat, {
	| gate=1 rate=1 amp=0.5 pan=0 out=0 |
	var bufnum, env, output;
	bufnum = "~/hihat.wav".b;
	env = Env.asr(0.01, 1, 0.01).kr(2, gate);
	output = PlayBuf.ar(2, bufnum, rate*BufRateScale.kr(bufnum));
	Out.ar(out, Balance2.ar(output[0], output[1], pan, env * amp));
}).add;
```

...It's that easy!

## Current Issues

- Freeing Bdefs/Buffers doesn't work yet.
- No equivalent of allocConsecutive is provided yet (in the future, you'll be able to provide an Array for Bdef.new's value parameter, and the items of the Array will be loaded in consecutive Buffers).
- If you specify numChannels:1 on a file that has more than 1 channel, only the first channel is loaded. Ideally, the channels would be mixed together.
- Only 1- and 2-channel Buffers and files are supported.
- No helpfile yet.
- Sclang will hang while a file is being converted to wav.
- Will load 2 copies of a buffer if you do this:

```supercollider
"/path/to/foo.wav".b;
Bdef(\bar, "/path/to/foo.wav");
```
