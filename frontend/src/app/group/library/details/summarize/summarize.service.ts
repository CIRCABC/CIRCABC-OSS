import { Service } from '@angular/core';
import { environment } from 'environments/environment';

/**
 * Shape of the payload returned by the AI agent's summarization endpoint.
 */
export interface SummarizeResponse {
  /** Identifier of the library node that was summarized. */
  nodeId: string;
  /** The generated textual summary of the node's content. */
  summary: string;
}

/**
 * Root-provided service that requests AI-generated summaries for library
 * document nodes from the configured external AI agent.
 *
 * The service talks directly to the AI agent endpoint defined by
 * `environment.aiAgentUrl` using the Fetch API, authenticating each request
 * with the CIRCABC session ticket stored in `sessionStorage`. It is only
 * usable when an AI agent URL is configured for the current environment.
 */
@Service()
export class SummarizeService {
  /**
   * Indicates whether the summarization feature is available in the current
   * environment, i.e. whether an AI agent URL has been configured.
   *
   * @returns `true` when `environment.aiAgentUrl` is set, `false` otherwise.
   */
  get isEnabled(): boolean {
    return !!environment.aiAgentUrl;
  }

  /**
   * Requests an AI-generated summary for the given library node.
   *
   * Sends a `POST` request to the AI agent's `/summarize/{nodeId}` endpoint,
   * authenticating with the CIRCABC ticket read from `sessionStorage`. The
   * request is aborted after a 5-minute (300 000 ms) timeout.
   *
   * @param nodeId - Identifier of the library node to summarize.
   * @returns A promise resolving to the {@link SummarizeResponse} containing
   * the node id and its generated summary.
   * @throws Error If the HTTP response is not successful (non-2xx status).
   */
  async summarize(nodeId: string): Promise<SummarizeResponse> {
    const ticket = sessionStorage.getItem('ticket') ?? '';
    const response = await fetch(
      `${environment.aiAgentUrl}/summarize/${nodeId}`,
      {
        method: 'POST',
        headers: { 'X-Circabc-Ticket': ticket },
        signal: AbortSignal.timeout(300_000),
      }
    );
    if (!response.ok) {
      throw new Error(`Summarize failed: ${response.status}`);
    }
    return response.json() as Promise<SummarizeResponse>;
  }
}
