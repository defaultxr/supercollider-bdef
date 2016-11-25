+ Symbol {
	b {
		^Bdef(this).buffer;
	}
	bd {
		^Bdef(this);
	}
	bdef {
		^Bdef(this);
	}
}

+ String {
	b {
		| startFrame=0 |
		^this.bd(numChannels:2, startFrame:startFrame).buffer;
	}
	bm {
		| startFrame=0 |
		^this.bd(numChannels:1, startFrame:startFrame).buffer;
	}
	bw {
		^this.bd(numChannels:1, wavetable:true).buffer;
	}
	bd {
		| item numChannels wavetable startFrame=0 |
		^Bdef(this, item, numChannels, wavetable, startFrame:startFrame);
	}
	bdef {
		| item numChannels wavetable startFrame=0 |
		^Bdef(this, item, numChannels, wavetable, startFrame:startFrame);
	}
	convertToWav {
		/*
			If the file specified by this string is not supported for loading by SuperCollider, it is converted to wav using ffmpeg, and then the path to the wav file is returned.
		*/
		// FIX: should check to see if the wav actually exists after conversion - in case the conversion fails somehow!
		var dir = (Platform.defaultTempDir ++ "supercollider").mkdir; // should be a temporary directory so the converted files don't stick around and waste your harddrive space!
		var file = this.standardizePath;
		var output = dir +/+ file.basename.splitext[0] ++ ".wav";
		if([\wav, \aif, \aiff].includes((file.basename.splitext[1] ? "").toLower.asSymbol), { // things that don't need to be converted
			^file;
		}, {
			if(File.exists(output), { // the output file already exists(?), so we do nothing and just return the path to it.
				^output;
			}, { // if the output file doesn't exist, we have to convert it.
				("Converting " ++ file ++ "...").postln;
				("ffmpeg -i \"" ++ file ++ "\" \"" ++ output ++ "\" 2>/dev/null").systemCmd;
				("Done converting " ++ file ++ ".").postln;
				^output;
			});
		});
	}
}

