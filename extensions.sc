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
}

