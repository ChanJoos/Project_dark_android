package com.projectdark.mobile;

/**
 * Profession strategy. Decisions submit existing Resolver actions; they never deal direct damage.
 */
interface CompanionAgent {
  void decide(CompanionAgentContext context, RuntimeState.Monster target);

  default void defend(CompanionAgentContext c) {
    RuntimeState.Monster target = c.attacker();
    if (target != null && c.melee(target) && c.safeTarget(target)) c.basic(target);
  }
}
