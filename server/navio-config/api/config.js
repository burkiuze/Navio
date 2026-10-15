// Public runtime config for the Navio Android app. Values live in this project's env vars,
// so the app ships only this URL.
export const config = { runtime: 'edge' };

export default function handler() {
  const body = {
    clerkFrontendApi: process.env.NAVIO_CLERK_FAPI || '',
    aiProxyBase: process.env.NAVIO_AI_PROXY || '',
  };
  return new Response(JSON.stringify(body), {
    headers: {
      'content-type': 'application/json',
      'access-control-allow-origin': '*',
      'cache-control': 's-maxage=300',
    },
  });
}
