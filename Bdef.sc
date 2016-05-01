// FIX: for some reason it won't let me put the String and Symbol shortcut extensions in here (i.e. "~/bla.wav".b, \x.bd, etc)
// TODO: make a 'free' method
// TODO: allow user to specify an array of items, for allocConsecutive

Bdef { // Buffer definition, a la Ndef, Pdef, Tdef, etc., except it's for Buffers, obviously
	var <key;
	classvar <>all;
	*initClass {
		all = Dictionary.new;
	}
	*hasGlobalDictionary { ^true; }
	*new {
		| key item numChannels=2 wavetable |
		/* you can create a new Bdef in a variety of ways:
			* Bdef(\name, Env([0.5, 1, 0.5, 0, 0.5], 0.25!4, [-3, 3, -3, 3])); // i.e. for making a wavetable buffer.
			* Bdef(\name, "~/foo.wav"); // i.e. for loading a sound from disk.
			* Bdef("~/foo.wav"); // also works for loading a sound from disk!
			* Bdef(\name, "~/bar.mp3"); // automatically converts songs into a SuperCollider-usable format!
			* Bdef(\table, "~/baz.wav", wavetable:true); // load a sound from disk as a wavetable! (defaults to Wavetable-compatible buffer size nearest to the soundfile's actual size.)
			* Bdef(\table, "~/baz.wav", wavetable:2048); // load a sound from disk as a wavetable with a buffer of a specific size
			* Bdef(1); // reference a Buffer by its number
			* Bdef(\name, 44100); // create an empty buffer with a length of 44100 frames
		*/
		// ^super.new.init(key, item, numChannels, wavetable);
		if(item.isNil, {
			if(key.isKindOf(String), { // assume strings are paths. Anytime a file is loaded, it's stored under its own name in the 'all' dict.
				key = key.p.convertToWav;
				if(wavetable.isNil, {
					if(all[key].isNil, {
						Bdef.read(path:key, numChannels:numChannels);
					});
				}, { // load as wavetable
					if(all[key].isNil, {
						all.put(key, Bdef.readAsWavetable(
							path:key,
							numChannels:numChannels,
							size:if(wavetable.isNumber, wavetable, nil),
						));
					});
					// , {
					// 	if(wavetable.isNumber and: { all[key].numFrames != wavetable }, {
					// 		all[key].free;
					// 		all.put(key, Bdef.readAsWavetable(
					// 			path:key,
					// 			numChannels:numChannels,
					// 			size:if(wavetable.isNumber, wavetable, nil),
					// 		));
					// 	});
					// });
				});
			});
		}, { // set the value cuz item was provided
			case(
				{ item.isKindOf(String) }, { // we assume the string is a path.
					item = item.p; // FIX: check/handle if trying to load as a wavetable!
					if(wavetable.isNil, {
						if(all[item].isNil, {
							Bdef.read(path:item, numChannels:numChannels);
						});
					}, {
						Bdef(item, numChannels:numChannels, wavetable:wavetable);
					});
					all.put(key, item);
				},
				{ item.isKindOf(Env) }, {
					// FIX - make it possible to load the Env as a regular buffer instead of a wavetable (though not by default)
					var asWave;
					if(all[key].notNil and: { wavetable.isNumber } and: { all[key].numFrames != wavetable }, { // resizing it
						all[key].free;
						all[key] = nil;
					});
					wavetable = if(wavetable.isNumber, wavetable, 1024);
					asWave = item.asSignal(wavetable/2+1).asWavetableNoWrap;
					if(all[key].isNil, {
						all.put(key, Buffer.loadCollection(Server.default, asWave));
					}, {
						all.at(key).sendCollection(asWave);
					});
				},
				{ item.isKindOf(Number) }, {
					if(all[key].isNil, {
						all[key] = Buffer.alloc(Server.default, item, numChannels);
					});
				},
			);
		});
		^super.new.init(key);
	}
	*collect {
		| path=("sounds/*") |
		var sfs = SoundFile.collect(path.p);
		^sfs.collect({
			| sf |
			Bdef(sf.path);
		});
	}
	*read { // FIX: just load the file into a Buffer and return the Buffer, don't do anything with the 'all' dict here.
		| server=(Server.default) path startFrame=0 numFrames=(-1) action bufnum numChannels |
		var sf, wavpath;
		path = path.p;
		wavpath = path.convertToWav;
		sf = SoundFile.openRead(wavpath);
		if(all[path].notNil, {
			all[path].free;
		});
		all[path] = if(numChannels.isNil, {
			Buffer.read(server, wavpath, startFrame, numFrames, action, bufnum);
		}, {
			switch(numChannels,
				1, { // FIX: do a mixdown instead of just taking one channel maybe?
					Buffer.readChannel(server, wavpath, startFrame, numFrames, [0], action, bufnum);
				},
				2, {
					switch(sf.numChannels,
						1, {
							Buffer.readChannel(server, wavpath, startFrame, numFrames, [0, 0], action, bufnum);
						},
						2, {
							Buffer.readChannel(server, wavpath, startFrame, numFrames, [0, 1], action, bufnum);
						},
					);
				},
			);
		});
	}
	*readAsWavetable {
		| server=(Server.default) path action numChannels=1 size |
		var sf, fa, po2;
		sf = SoundFile.new;
		sf.openRead(path);
		fa = FloatArray.newClear(sf.numFrames);
		sf.readData(fa);
		sf.close;
		po2 = (2**(1..15));
		size = (size ? sf.numFrames);
		if(po2.includes(size).not, {
			var index = (po2.indexOfGreaterThan(size) ? 10);
			"Rounding wavetable buffer size to the nearest power of 2.".warn;
			size = po2[index.clip(0, 14)];
		});
		if(size != sf.numFrames, {
			fa = fa.resamp1(size);
		});
		fa = fa.as(Signal).asWavetable;
		^Buffer.loadCollection(server, fa, numChannels ? 1, action);
	}
	*at {
		| key |
		^if(key.isNumber, {
			Buffer.at(key);
		}, {
			this.all.at(key);
		});
	}
	*free {
		| key |
		var buf = all.at(key);
		if(buf.isNil, {
			("Nothing found at key " ++ key.asString).warn;
		}, {
			var removeKeys = [];
			all.keysValuesDo({
				| k val |
				if(val == buf, {
					removeKeys = removeKeys ++ [k];
				});
			});
			removeKeys.do({
				| k |
				all.removeAt(k);
			});
			buf.free;
		});
	}
	init {
		| argKey |
		key = argKey;
	}
	storeOn {
		| stream |
		var char = if(this.key.isKindOf(String), "\"", "'");
		stream << "Bdef(" << char << this.key.asString << char << ")";
	}
	printOn {
		| stream |
		^this.storeOn(stream);
	}
	buffer {
		var at = Bdef.at(this.key);
		^case(
			{ at.isNil }, {
				nil;
			},
			{ at.isKindOf(String) or: { at.isKindOf(Symbol) } }, {
				Bdef.at(at);
			},
			true, {
				at;
			},
		);
	}
	item {
		^this.buffer;
	}
	b {
		^this.buffer;
	}
	clear {
		this.buffer.clear;
	}
	server {
		^Server.default;
	}
	bufnum {
		^this.bufnum;
	}
	numFrames {
		^this.buffer.numFrames;
	}
	numChannels {
		^this.buffer.numChannels;
	}
	sampleRate {
		^this.buffer.sampleRate;
	}
	path {
		^this.buffer.path;
	}
	free {
		"Not done yet".warn; // FIX
		this.buffer.free;
	}
	play {
		this.buffer.play;
	}
}