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
		^this.bd(numChannels:2).buffer;
	}
	bm {
		^this.bd(numChannels:1).buffer;
	}
	bw {
		^this.bd(numChannels:1, wavetable:true).buffer;
	}
	bd {
		| item numChannels wavetable |
		^Bdef(this, item, numChannels, wavetable);
	}
	bdef {
		| item numChannels wavetable |
		^Bdef(this, item, numChannels, wavetable);
	}
}

