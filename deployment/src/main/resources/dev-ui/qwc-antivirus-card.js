import { LitElement, html, css} from 'lit';
import { pages } from 'build-time-data';
import 'qwc/qwc-extension-link.js';

const NAME = "Antivirus";
export class QwcAntivirusCard extends LitElement {

    static styles = css`
      .identity {
        display: flex;
        justify-content: flex-start;
      }

      .description {
        padding-bottom: 10px;
      }

      .logo {
        padding-bottom: 10px;
        margin-right: 5px;
      }

      .card-content {
        color: var(--lumo-contrast-90pct);
        display: flex;
        flex-direction: column;
        justify-content: flex-start;
        padding: 2px 2px;
        height: 100%;
      }

      .card-content slot {
        display: flex;
        flex-flow: column wrap;
        padding-top: 5px;
      }
    `;

    static properties = {
        description: {type: String}
    };

    constructor() {
        super();
    }

    connectedCallback() {
        super.connectedCallback();
    }

    render() {
        return html`<div class="card-content" slot="content">
            <div class="identity">
                <div class="logo">
                    <img src="data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz48c3ZnIHZlcnNpb249IjEuMSIgaWQ9IkxheWVyXzEiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHg9IjBweCIgeT0iMHB4IiB2aWV3Qm94PSIwIDAgMTExLjgxIDEyMi44OCIgc3R5bGU9ImVuYWJsZS1iYWNrZ3JvdW5kOm5ldyAwIDAgMTExLjgxIDEyMi44OCIgeG1sOnNwYWNlPSJwcmVzZXJ2ZSI+PHN0eWxlIHR5cGU9InRleHQvY3NzIj48IVtDREFUQVsNCgkuc3Qwe2ZpbGwtcnVsZTpldmVub2RkO2NsaXAtcnVsZTpldmVub2RkO2ZpbGw6IzQzOThEMTt9DQoJLnN0MXtmaWxsLXJ1bGU6ZXZlbm9kZDtjbGlwLXJ1bGU6ZXZlbm9kZDtmaWxsOiNFRDVDNTY7fQ0KCS5zdDJ7ZmlsbC1ydWxlOmV2ZW5vZGQ7Y2xpcC1ydWxlOmV2ZW5vZGQ7ZmlsbDojRkZGRkZGO30NCgkuc3Qze2ZpbGw6IzQzOThEMTt9DQpdXT48L3N0eWxlPjxnPjxwYXRoIGNsYXNzPSJzdDAiIGQ9Ik01NS43MSwwYzIwLjg1LDEzLjIxLDM5LjY4LDE5LjQ3LDU1Ljg1LDE3Ljk5YzIuODIsNTcuMS0xOC4yNiw5MC44Mi01NS42MywxMDQuODkgQzE5Ljg0LDEwOS43MS0xLjUsNzcuNDQsMC4wOCwxNy4xMkMxOS4wNiwxOC4xMiwzNy42NywxNC4wMSw1NS43MSwwTDU1LjcxLDBMNTUuNzEsMEw1NS43MSwweiIvPjxwYXRoIGNsYXNzPSJzdDIiIGQ9Ik01NS43NCw5LjYxYzE3LjU4LDExLjE1LDMzLjQ3LDE2LjQyLDQ3LjEsMTUuMTdjMi4zOCw0OC4xNi0xNS40MSw3Ni42LTQ2LjkyLDg4LjQ3IGMtMzAuNDMtMTEuMTEtNDguNDMtMzguMzItNDcuMS04OS4yQzI0LjgzLDI0LjksNDAuNTMsMjEuNDQsNTUuNzQsOS42MUw1NS43NCw5LjYxTDU1Ljc0LDkuNjFMNTUuNzQsOS42MXoiLz48cGF0aCBjbGFzcz0ic3QxIiBkPSJNNjIuNDEsNDUuODJjLTAuOTMsMC0xLjY4LDAuNzUtMS42OCwxLjY4YzAsMC45MywwLjc1LDEuNjgsMS42OCwxLjY4YzAuOTMsMCwxLjY4LTAuNzUsMS42OC0xLjY4IEM2NC4wOSw0Ni41OCw2My4zNCw0NS44Miw2Mi40MSw0NS44Mkw2Mi40MSw0NS44Mkw2Mi40MSw0NS44MnogTTU2Ljc0LDM3LjI1YzEuODQsMCwzLjU5LDAuNTUsNS4yMiwxLjU1IGMwLjA0LTAuMDQsMC4wOS0wLjA3LDAuMTQtMC4xYzMuMTQtMi4xNCw3LjI0LTUuOTUsNy4zMS0xMC4wMmMwLjAyLTEuMDYsMC44OS0xLjksMS45NS0xLjg5YzEuMDYsMC4wMiwxLjkxLDAuODksMS44OSwxLjk1IGMtMC4wOSw1LjE3LTQuMjYsOS43NS04LjI2LDEyLjYzYzEuNzMsMS45LDMuMiw0LjM4LDQuMzMsNy4yN2wtNC45LDQuNzdjLTIuMjgsMC4yOS00Ljg4LDAuNDYtNy41MywwLjQ5IGMtNS4wNywwLjA1LTEwLjI4LTAuNDQtMTMuNzUtMS42MWMtMC4wNi0wLjAyLTAuMTItMC4wMy0wLjE3LTAuMDVjMS4xNi00LjQsMy4wNS04LjE1LDUuNDMtMTAuNzljLTQuMDUtMi44OS04LjI4LTcuNDktOC4zOC0xMi43MSBjLTAuMDItMS4wNiwwLjgzLTEuOTQsMS44OS0xLjk1YzEuMDYtMC4wMiwxLjk0LDAuODMsMS45NSwxLjg5YzAuMDcsNC4wNyw0LjE3LDcuODcsNy4zMSwxMC4wMmMwLjA4LDAuMDYsMC4xNSwwLjExLDAuMjIsMC4xNyBDNTMuMDcsMzcuODMsNTQuODcsMzcuMjUsNTYuNzQsMzcuMjVMNTYuNzQsMzcuMjVMNTYuNzQsMzcuMjV6IE04NS4xMyw1My43NmwzLjkyLDUuMzRjMC42MywwLjg2LDAuNDUsMi4wNi0wLjQxLDIuNyBjLTAuODYsMC42My0yLjA2LDAuNDUtMi42OS0wLjQxbC0zLjYxLTQuOTFMODUuMTMsNTMuNzZMODUuMTMsNTMuNzZ6IE03NS4wOCw2My41NWg2LjA5YzAuNjksMCwxLjMsMC4zNywxLjY1LDAuOTJsNi4wNSw3Ljk3IGMwLjY0LDAuODUsMC40OCwyLjA1LTAuMzcsMi43cy0yLjA1LDAuNDgtMi43LTAuMzdsLTUuNTgtNy4zNWgtOC41N2MtMC4wNywwLTAuMTQsMC0wLjIxLTAuMDFjLTAuMTUsMS4xOS0wLjM1LDIuMzYtMC42MSwzLjQ5IGg2LjQ1YzEuMDcsMCwxLjkzLDAuODYsMS45MywxLjkzdjEzLjUzYzAsMS4wNy0wLjg2LDEuOTMtMS45MywxLjkzYy0xLjA3LDAtMS45My0wLjg2LTEuOTMtMS45M3YtMTEuNmgtNS42MSBjLTIuMzYsNi43Ny02LjU5LDExLjUyLTExLjUsMTIuMzN2LTcuMTNMNzUuMDgsNjMuNTVMNzUuMDgsNjMuNTV6IE01OC4yMyw1OS40NnYtMi4yOGMwLjgzLTAuMDIsMS42NS0wLjA2LDIuNDYtMC4xMUw1OC4yMyw1OS40NiBMNTguMjMsNTkuNDZ6IE01NC45Miw4Ny4wM2MtMC45OS0wLjItMS45NC0wLjU2LTIuODYtMS4wNmwyLjg2LTIuNzlMNTQuOTIsODcuMDNMNTQuOTIsODcuMDNMNTQuOTIsODcuMDN6IE00Mi41MSw3NC43NmgtNC41NyB2NC40NWwtMy44NiwzLjc2bDAtMTAuMTRjMC0xLjA3LDAuODYtMS45MywxLjkzLTEuOTNoNi42NWMtMC4yNS0xLjE0LTAuNDYtMi4zMS0wLjYxLTMuNTJjLTAuMTMsMC4wMy0wLjI3LDAuMDQtMC40MSwwLjA0aC04LjU2IGwtNS41OCw3LjM1Yy0wLjY0LDAuODUtMS44NSwxLjAxLTIuNjksMC4zN2MtMC44NS0wLjY0LTEuMDEtMS44NS0wLjM3LTIuN2w2LjA1LTcuOTZjMC4zNC0wLjU1LDAuOTUtMC45MiwxLjY1LTAuOTJoOS41MiBjMC4wNCwwLDAuMDgsMCwwLjEyLDAuMDFjLTAuMDItMC40NC0wLjAyLTAuODgtMC4wMi0xLjMzYzAtMC44NywwLjAzLTEuNzMsMC4wOC0yLjU4Yy0wLjExLTAuMDQtMC4yMS0wLjA5LTAuMzEtMC4xNGwtOC45NC01LjA5IGwtNS4yMSw3LjFjLTAuNjMsMC44Ni0xLjg0LDEuMDUtMi43LDAuNDFjLTAuODYtMC42My0xLjA1LTEuODMtMC40MS0yLjY5bDYuMjMtOC40OGwwLDBjMC41Ny0wLjc4LDEuNjUtMS4wMiwyLjUtMC41M2w4LjU4LDQuODggYzAuMTYsMC4xNCwwLjM1LDAuMjUsMC41NiwwLjMyaDAuMDFsMC4xNywwLjFsMC4wMS0wLjA0YzMuMzQsMS4wOSw3Ljk3LDEuNjIsMTIuNjMsMS43MXY1LjQ5TDQyLjUxLDc0Ljc2TDQyLjUxLDc0Ljc2eiBNNTEuMDgsNDUuODJjLTAuOTMsMC0xLjY4LDAuNzUtMS42OCwxLjY4YzAsMC45MywwLjc1LDEuNjgsMS42OCwxLjY4YzAuOTMsMCwxLjY4LTAuNzUsMS42OC0xLjY4IEM1Mi43Nyw0Ni41OCw1Mi4wMSw0NS44Miw1MS4wOCw0NS44Mkw1MS4wOCw0NS44Mkw1MS4wOCw0NS44MnoiLz48cGF0aCBjbGFzcz0ic3QzIiBkPSJNMTAxLjc5LDIyLjQ0YzEuMTctMS4xMSwzLjAxLTEuMDYsNC4xMiwwLjFjMS4xMSwxLjE3LDEuMDYsMy4wMS0wLjEsNC4xMmwtNzUuMDcsNzQuNDkgYy0xLjE2LDEuMTEtMy4wMSwxLjA2LTQuMTItMC4xYy0xLjExLTEuMTctMS4wNi0zLjAxLDAuMS00LjEyTDEwMS43OSwyMi40NEwxMDEuNzksMjIuNDR6Ii8+PC9nPjwvc3ZnPg=="
                                       alt="${NAME}" 
                                       title="${NAME}"
                                       width="32" 
                                       height="32">
                </div>
                <div class="description">${this.description}</div>
            </div>
            ${this._renderCardLinks()}
        </div>
        `;
    }

    _renderCardLinks(){
        return html`${pages.map(page => html`
                            <qwc-extension-link slot="link"
                                extensionName="${NAME}"
                                iconName="${page.icon}"
                                displayName="${page.title}"
                                staticLabel="${page.staticLabel}"
                                dynamicLabel="${page.dynamicLabel}"
                                streamingLabel="${page.streamingLabel}"
                                path="${page.id}"
                                ?embed=${page.embed}
                                externalUrl="${page.metadata.externalUrl}"
                                webcomponent="${page.componentLink}" >
                            </qwc-extension-link>
                        `)}`;
    }

}
customElements.define('qwc-antivirus-card', QwcAntivirusCard);