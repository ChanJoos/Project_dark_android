package com.projectdark.mobile;

/**
 * Game-owned semantic animation contract. Visual chooses source frames; Game only requests intent.
 */
public enum AnimationAction {
  PUNCH,
  SWING,
  THRUST,
  THROW,
  KICK,
  CAST,
  SKILL
}
