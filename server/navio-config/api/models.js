// Curated list of strong, tool-capable OpenRouter models for Navio's model picker.
export const config = { runtime: 'edge' };

const PROVIDERS = ['openai', 'anthropic', 'google', 'x-ai', 'deepseek', 'qwen', 'moonshotai', 'mistralai'];
const PER_PROVIDER = 3;

export default async function handler() {
  const r = await fetch('https://openrouter.ai/api/v1/models');
  const j = await r.json().catch(() => ({ data: [] }));
  const picked = [];
  for (const provider of PROVIDERS) {
    (j.data || [])
      .filter((m) => m.id && m.id.startsWith(`${provider}/`) && !m.id.includes(':'))
      .filter((m) => (m.supported_parameters || []).includes('tools'))
      .filter((m) => !/(audio|image|banana|vision|omni|embed|guard|search-preview|codex|-code|instruct$)/i.test(m.id))
      .sort((a, b) => (b.created || 0) - (a.created || 0))
      .slice(0, PER_PROVIDER)
      .forEach((m) => picked.push({ id: m.id, name: m.name || m.id }));
  }
  const data = [{ id: 'openrouter/auto', name: 'Auto (en iyi model)' }, ...picked];
  return new Response(JSON.stringify({ data }), {
    headers: { 'content-type': 'application/json', 'access-control-allow-origin': '*', 'cache-control': 's-maxage=3600' },
  });
}
