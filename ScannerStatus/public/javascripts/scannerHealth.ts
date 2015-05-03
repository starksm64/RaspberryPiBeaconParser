module ScannerStatus {
    export class ScannerHealth {
        private scannerID:string;
        private time:number;
        private lastStatus:Map<string,string>;
        private alertSound:HTMLMediaElement;
        private alertClearSound:HTMLMediaElement;
        private alertClearSounded:boolean;
        // Seconds since the last status msg was seen
        sinceLastStatus:number;

        constructor(scannerID:string) {
            this.scannerID = scannerID;
            this.lastStatus = new Map<string,string>();
            this.sinceLastStatus = 0;
            this.alertSound = new Audio('alert.wav');
            this.alertClearSound = new Audio('alertclear.wav');
            this.alertClearSounded = true;
        }

        public getScannerID():string {
            return this.scannerID;
        }

        public setScannerID(value:string) {
            this.scannerID = value;
        }

        public getTime():number {
            return this.time;
        }

        public setTime(value:number) {
            this.time = value;
        }

        public getSinceLastStatus():number {
            return this.sinceLastStatus;
        }

        public setSinceLastStatus(seconds:number) {
            this.sinceLastStatus = seconds;
        }

        public updateSinceLastStatus(now:number) {
            this.sinceLastStatus = (now - this.time) / 1000;
            if (this.sinceLastStatus > 60) {
                this.alertClearSounded = false;
                this.alertSound.play();
            } else {
                this.alertSound.pause();
                if (!this.alertClearSounded) {
                    this.alertClearSounded = true;
                    this.alertClearSound.play();
                }
            }
        }

        public getAlert():boolean {
            return this.sinceLastStatus > 60;
        }

        public getLastStatus():Map<string,string> {
            return this.lastStatus;
        }

        public setLastStatus(value:Map<string,string>) {
            this.lastStatus = value;
        }

        public getProperty(key:string):string {
            return this.lastStatus.get(key);
        }

        public setProperty(key, value:string) {
            this.lastStatus.set(key, value);
        }
    }
}