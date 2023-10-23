import { QwcServerLog} from 'qwc-server-log';

/**
 * This component filter the log to only show ClamAV related entries.
 */
export class QwcClamAVLog extends QwcServerLog {

    doLogEntry(entry){
        if (entry.loggerName && entry.loggerName.includes("ClamAVContainer")) {
            return true;
        }
        return false;
    }
}

customElements.define('qwc-antivirus-log', QwcClamAVLog);